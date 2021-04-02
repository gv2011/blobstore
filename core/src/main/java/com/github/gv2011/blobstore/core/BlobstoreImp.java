package com.github.gv2011.blobstore.core;

import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.callWithCloseable;
import static com.github.gv2011.util.ex.Exceptions.notYetImplemented;
import static com.github.gv2011.util.icol.ICollections.setFrom;
import static com.github.gv2011.util.icol.ICollections.setOf;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.github.gv2011.blobstore.Blobstore;
import com.github.gv2011.blobstore.StoreResult;
import com.github.gv2011.blobstore.StoreResult.ResultCode;
import com.github.gv2011.util.BeanUtils;
import com.github.gv2011.util.StreamUtils;
import com.github.gv2011.util.XStream;
import com.github.gv2011.util.bytes.Hash256;
import com.github.gv2011.util.bytes.HashAndSize;
import com.github.gv2011.util.bytes.HashFactory;
import com.github.gv2011.util.icol.ISet;
import com.github.gv2011.util.icol.Opt;

final class BlobstoreImp implements Blobstore{
  
  private final HashFactory hashFactory = HashFactory.INSTANCE.get();
  
  private final Path storeDir;
  private final Path tmpDir;
  
  private final Object lock = new Object();
  
  private final Map<Hash256,Integer> reading = new HashMap<>();
  
  BlobstoreImp(Path storeDir) {
    this.storeDir = storeDir;
    tmpDir = storeDir.resolve("tmp");
    call(()->Files.createDirectories(tmpDir));
  }

  @Override
  public StoreResult store(InputStream data) {
    return storeInternal(data, Opt.empty(), Opt.empty());
  }
  
  private StoreResult storeInternal(InputStream data, Opt<Hash256> hash, Opt<Long> size) {
    final Path tmpFile = call(()->Files.createTempFile(tmpDir, "write-", ""));
    HashAndSize hashAndSize = callWithCloseable(()->new DeflaterOutputStream(Files.newOutputStream(tmpFile)), out->{
      return hashFactory.hash256(()->data, out);
    });
    final Set<ResultCode> resultCodes = EnumSet.noneOf(ResultCode.class);
    size.ifPresent(s->{
      if(!hashAndSize.size().equals(s)) resultCodes.add(ResultCode.WRONG_SIZE);
    });
    hash.ifPresent(h->{
      if(!hashAndSize.hash().equals(h)) resultCodes.add(ResultCode.WRONG_HASH);
    });
    if(resultCodes.isEmpty()){
      resultCodes.addAll(storeTmpFile(tmpFile, hashAndSize));
    }
    else{
      call(()->Files.delete(tmpFile));
    }
    return BeanUtils.beanBuilder(StoreResult.class)
      .set(StoreResult::hash).to(Opt.of(hashAndSize.hash()))
      .set(StoreResult::size).to(Opt.of(hashAndSize.size()))
      .set(StoreResult::resultCodes).to(setFrom(resultCodes))
      .build()
    ;
  }

  private ISet<ResultCode> storeTmpFile(final Path tmpFile, HashAndSize hashAndSize) {
    synchronized(lock){
      Path location = getLocation(hashAndSize.hash());
      if(Files.exists(location)){
        final long tmpFileSize = call(()->Files.size(tmpFile));
        call(()->Files.delete(tmpFile));
        return 
          call(()->Files.size(location))==tmpFileSize
          ? setOf(ResultCode.OK, ResultCode.EXISTS)
          : setOf(ResultCode.WRONG_SIZE, ResultCode.EXISTS)
        ;
      }
      else{
        call(()->{
          Files.createDirectories(location.getParent());
          Files.move(tmpFile, location);
        });
        return setOf(ResultCode.OK);
      }
    }
  }
  
  
  private Path getLocation(Hash256 hash) {
    String name = hash.content().toHex();
    return storeDir.resolve(Paths.get(
      name.substring(0, 2),
      name.substring(2, 4),
      name.substring(4, 6),
      name
    ));
  }

  @Override
  public StoreResult store(InputStream data, Opt<Hash256> hash, Opt<Long> size) {
    boolean done = false;
    if(hash.isPresent()){
      synchronized(lock){
        final Path location = getLocation(hash.get());
        if(Files.exists(location)){
          done = true;
        }
      }
    }
    if(done){
      call(data::close);
      return BeanUtils.beanBuilder(StoreResult.class)
        .set(StoreResult::hash).to(hash)
        .set(StoreResult::size).to(size)
        .set(StoreResult::resultCodes).to(setOf(ResultCode.OK, ResultCode.EXISTS))
        .build()
      ;
    }
    else{
      return storeInternal(data, hash, size);
    }
  }
  
  @Override
  public Opt<InputStream> tryGet(Hash256 hash) {
    lock(hash);
    boolean unlockByClose = false;
    try{
      final Path location = getLocation(hash);
      if(Files.exists(location)){
        final AtomicBoolean closed = new AtomicBoolean();
        final Opt<InputStream> result = Opt.of(
          new FilterInputStream(call(()->Files.newInputStream(location))){
            @Override
            public void close() throws IOException {
              try{super.close();}
              finally{
                if(!closed.getAndSet(true)) unlock(hash);
              }
            }         
          }
        );
        unlockByClose = true;
        return result;
      }
      else{
        return Opt.empty();
      }
    }finally{
      if(!unlockByClose) unlock(hash);
    }
  }
  
  

  @Override
  public boolean remove(Hash256 hash) {
    synchronized(lock){
      while(reading.getOrDefault(hash, 0)>0) call(()->lock.wait());
      final Path location = getLocation(hash);
      return call(()->Files.deleteIfExists(location));
    }
  }

  private void lock(Hash256 hash) {
    synchronized(lock){
      int count = reading.getOrDefault(hash, 0);
      verify(count>=0);
      reading.put(hash, count+1);
      lock.notifyAll();
    }
  }
  
  private void unlock(Hash256 hash) {
    synchronized(lock){
      int count = reading.getOrDefault(hash, 0);
      verify(count>0);
      count--;
      if(count==0) reading.remove(hash);
      else reading.put(hash, count);
      lock.notifyAll();
    }
  }
  
  @Override
  public Opt<Long> tryGetSize(Hash256 hash) {
    return tryGet(hash).map(fileStream->{
      return callWithCloseable(()->new InflaterInputStream(fileStream), StreamUtils::count);
    });
  }
  
  @Override
  public XStream<HashAndSize> list(Hash256 startInclusive) {
    return notYetImplemented();
  }

}
