package com.github.gv2011.blobstore.core;

import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.callWithCloseable;
import static com.github.gv2011.util.icol.ICollections.toIList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.BiConsumer;

import com.github.gv2011.blobstore.Blobstore;
import com.github.gv2011.blobstore.DirEntry;
import com.github.gv2011.blobstore.Directory;
import com.github.gv2011.blobstore.DirectoryService;
import com.github.gv2011.blobstore.StoreResult;
import com.github.gv2011.util.BeanUtils;
import com.github.gv2011.util.beans.BeanBuilder;
import com.github.gv2011.util.beans.BeanType;
import com.github.gv2011.util.bytes.ByteUtils;
import com.github.gv2011.util.bytes.Bytes;
import com.github.gv2011.util.bytes.Hash256;
import com.github.gv2011.util.icol.IList;
import com.github.gv2011.util.icol.Opt;
import com.github.gv2011.util.json.JsonFactory;

final class DirectoryServiceImp implements DirectoryService{
  
  private final JsonFactory jf;
  private final BeanType<Directory> direcoryType = BeanUtils.typeRegistry().beanType(Directory.class);
  private final boolean includeModifiedTime = false;
  
  DirectoryServiceImp(JsonFactory jf) {
    this.jf = jf;
  }

  @Override
  public Directory readLocalDirectory(Path dir, BiConsumer<Path,Bytes> listener) {
    return BeanUtils.beanBuilder(Directory.class)
      .set(Directory::entries)
      .to(callWithCloseable(
        ()->Files.list(dir), 
        s->(IList<DirEntry>)s
          .sorted(Comparator.comparing(f->f.getFileName().toString()))
          .map(f->entryFor(f, listener))
          .collect(toIList())
      ))
      .build()
    ;
  }
  
  private DirEntry entryFor(Path file, BiConsumer<Path,Bytes> listener){
    final boolean isDirectory = Files.isDirectory(file);
    final Bytes bytes;
    if(isDirectory){
      bytes = ByteUtils.asUtf8(encode(readLocalDirectory(file, listener))).content();
    }
    else{
      bytes = ByteUtils.read(file);
    }
    listener.accept(file, bytes);
    final BeanBuilder<DirEntry> entry = BeanUtils.beanBuilder(DirEntry.class)
      .set(DirEntry::name).to(file.getFileName().toString())
      .set(DirEntry::isDirectory).to(isDirectory)
      .set(DirEntry::hash).to(bytes.hash().content())
      .set(DirEntry::size).to(bytes.longSize())
    ;
    if(includeModifiedTime){
      entry.set(DirEntry::modified).to(isDirectory 
        ? Opt.empty()
        : Opt.of(call(()->Files.getLastModifiedTime(file)).toInstant())
      );
    }
    return entry.build();
  }

  String encode(final Directory dir) {
    return jf.serialize(direcoryType.toJson(dir));
  }

  @Override
  public Hash256 store(Path fileOrDirectory, Blobstore store) {
    return entryFor(fileOrDirectory, (f,b)->store(b, store)).hash().asHash();
  }
  
  private void store(Bytes b, Blobstore store) {
    callWithCloseable(
      ()->b.openStream(),
      in->{
        final StoreResult r = store.store(in, Opt.of(b.hash()), Opt.of(b.longSize()));
        verify(r.resultCodes().contains(StoreResult.ResultCode.OK));
        return r.hash().get();
      }
    );
  }
}
