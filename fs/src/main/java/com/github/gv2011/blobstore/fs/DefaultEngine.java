package com.github.gv2011.blobstore.fs;

import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.callWithCloseable;

import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.NotDirectoryException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.InflaterInputStream;

import com.github.gv2011.blobstore.Blobstore;
import com.github.gv2011.blobstore.DirEntry;
import com.github.gv2011.blobstore.Directory;
import com.github.gv2011.util.BeanUtils;
import com.github.gv2011.util.StreamUtils;
import com.github.gv2011.util.bytes.ByteUtils;
import com.github.gv2011.util.bytes.Bytes;
import com.github.gv2011.util.bytes.Hash256;
import com.github.gv2011.util.icol.Opt;
import com.github.gv2011.util.icol.Path;
import com.github.gv2011.util.streams.InputStreamChannel;

final class DefaultEngine implements Engine{
  
  private final Blobstore blobstore;

  DefaultEngine(Blobstore blobstore) {
    this.blobstore = blobstore;
  }

  @Override
  public Opt<BasicFileAttributes> readAttributes(Path path) {
    return
      readDirEntry(path)
      .map(this::toAttributes)
    ;
  }

  @Override
  public Opt<SeekableByteChannel> newByteChannel(Path path) {
    return
      readDirEntry(path)
      .map(this::toByteChannel)
    ;
  }
  

  @Override
  public Opt<DirectoryStream<String>> newDirectoryStream(Path path) throws NotDirectoryException {
    final Opt<DirEntry> dirEntry = readDirEntry(path);
    if(dirEntry.map(e->!e.isDirectory()).orElse(false)) throw new NotDirectoryException("");
    return dirEntry
      .map(this::readDirectory)
      .map(d->new DirStream(d.entries()))
    ;
  }

  private Opt<DirEntry> readDirEntry(Path path) {
    return
      (path.isEmpty() ? Opt.<Path>empty() : Opt.of(path))
      .flatMap(p->
        tryGetRootDirectory(p.first())
        .flatMap(dir->
          path.size()==1 
          ? Opt.of(rootDirEntry(p.first(), dir))
          : resolve(dir, path.tail())
        )
      )
    ;
  }
  private Opt<Directory> tryGetRootDirectory(String root) {
    return blobstore.tryGet(hash(root))
    .map(in->{
      try{
        return readDirectory(in);
      }
      finally{call(in::close);}
    });
  }
  
  private DirEntry rootDirEntry(String rootPathElement, Directory dir){
    return BeanUtils.beanBuilder(DirEntry.class)
      .set(DirEntry::name).to(rootPathElement)
      .set(DirEntry::isDirectory).to(true)
      .set(DirEntry::hash).to(hash(rootPathElement).content())
      .build()
    ;
  }

  
  private BasicFileAttributes toAttributes(DirEntry entry){
    return new FileAttributes(entry);
  }
  
  private SeekableByteChannel toByteChannel(DirEntry entry){
    return new InputStreamChannel(
      ()->new InflaterInputStream(blobstore.tryGet(hash(entry.hash())).get()), 
      entry.size()
    );
  }
  
  private Opt<DirEntry> resolve(Directory dir, Path path){
    assert !path.isEmpty();
    return dir.entry(path.first())
      .flatMap(child->{
        if(path.size()==1) return Opt.of(child);
        else if(child.isDirectory()){
          return resolve(readDirectory(child), path.tail());
        }
        else{
          return Opt.empty();
        }
      })
    ;
  }
  
  private Directory readDirectory(DirEntry dirEntry){
    return 
      callWithCloseable(
        ()->blobstore.tryGet(hash(dirEntry.hash())).get(),
        in->(Directory)readDirectory(in)
      )
    ;
  }

  private Directory readDirectory(InputStream inputStream) {
    return
      BeanUtils.typeRegistry().beanType(Directory.class)
      .parse(StreamUtils.readText(()->new InflaterInputStream(inputStream)))
    ;
  }

  private Hash256 hash(String pathElement) {
    return hash(ByteUtils.asUtf8(pathElement).content().decodeBase64());
  }

  private Hash256 hash(Bytes raw) {
    return raw.asHash();
  }

}
