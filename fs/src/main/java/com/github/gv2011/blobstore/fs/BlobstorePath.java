package com.github.gv2011.blobstore.fs;

import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.Verify.verifyEqual;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.icol.ICollections.iCollections;
import static com.github.gv2011.util.icol.ICollections.toIList;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Comparator;
import java.util.stream.IntStream;

import com.github.gv2011.blobstore.BlobFactory;
import com.github.gv2011.util.UrlEncoding;
import com.github.gv2011.util.icol.IList;
import com.github.gv2011.util.icol.Path;

final class BlobstorePath implements java.nio.file.Path {
  
  private static final Comparator<BlobstorePath> COMP = Comparator.comparing((BlobstorePath p)->p.absolute).thenComparing(p->p.path);

  private final BlobstoreFs fileSystem;
  private final boolean absolute;
  private final Path path;

  BlobstorePath(BlobstoreFs fileSystem, URI uri) {
    this(
      fileSystem,
      true,
      UrlEncoding.decodePath(checkUri(uri).getPath())
    );
  }

  private static URI checkUri(URI uri) {
    verify(uri, u->u.getScheme().equalsIgnoreCase(BlobFactory.SCHEME));
    verify(uri, u->u.getAuthority()==null);
    verify(uri, u->u.getFragment()==null);
    verify(uri, u->u.getHost()==null);
    verifyEqual(uri.getPort(), -1);
    verify(uri, u->u.getUserInfo()==null);
    return uri;
  }

  BlobstorePath(BlobstoreFs fileSystem) {
    this(fileSystem, true, iCollections().emptyPath());
  }

  BlobstorePath(BlobstoreFs fileSystem, boolean absolute, Path path) {
    this.fileSystem = fileSystem;
    this.absolute = absolute;
    this.path = path;
  }

  @Override
  public FileSystem getFileSystem() {
    return fileSystem;
  }

  @Override
  public boolean isAbsolute() {
    return absolute;
  }

  @Override
  public BlobstorePath getRoot() {
    return fileSystem.rootPath;
  }

  @Override
  public BlobstorePath getFileName() {
    return path.isEmpty() ? null : getName(path.size()-1);
  }

  @Override
  public BlobstorePath getParent() {
    return path.isEmpty() ? null : createAbsolute(path.subList(0, path.size()-1));
  }

  @Override
  public int getNameCount() {
    return path.size();
  }

  @Override
  public BlobstorePath getName(int index) {
    return subpath(index, index+1);
  }

  @Override
  public BlobstorePath subpath(int beginIndex, int endIndex) {
    return createRelative(path.subList(beginIndex, endIndex));
  }

  @Override
  public boolean startsWith(java.nio.file.Path other) {
    if(!fileSystem.equals(other.getFileSystem())) return false;
    else return path.startsWith(((BlobstorePath)other).path);
  }

  @Override
  public boolean endsWith(java.nio.file.Path other) {
    if(!fileSystem.equals(other.getFileSystem())) return false;
    else return path.endsWith(((BlobstorePath)other).path);
  }

  @Override
  public BlobstorePath normalize() {
    return this;
  }

  @Override
  public java.nio.file.Path resolve(java.nio.file.Path other) {
    if(other.isAbsolute()) return other;
    else if(other.getNameCount()==0) return this;
    else{
      return new BlobstorePath(fileSystem, absolute, path.join(asList(other)));
    }
  }

  private IList<String> asList(java.nio.file.Path filePath) {
    return IntStream.range(0, filePath.getNameCount()).mapToObj(i->filePath.getName(i).toString()).collect(toIList());
  }

  @Override
  public BlobstorePath relativize(java.nio.file.Path other) {
    if(!fileSystem.equals(other.getFileSystem())) throw new IllegalArgumentException();
    else{
      final BlobstorePath o = (BlobstorePath) other;
      if(!o.path.startsWith(path)) throw new IllegalArgumentException();
      else return createRelative(o.path.subList(path.size(), o.path.size()));
    }
  }

  @Override
  public URI toUri() {
    return call(()->new URI(BlobFactory.SCHEME, null, UrlEncoding.encodePath(path), null));
  }

  @Override
  public BlobstorePath toAbsolutePath() {
    return absolute ? this : createAbsolute(path);
  }

  @Override
  public BlobstorePath toRealPath(LinkOption... options) throws IOException {
    return toAbsolutePath();
  }

  @Override
  public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int compareTo(java.nio.file.Path other) {
    if(!fileSystem.equals(other.getFileSystem())) throw new IllegalArgumentException();
    else return COMP.compare(this, (BlobstorePath) other);
  }
  
  private BlobstorePath createRelative(Path path){
    return new BlobstorePath(fileSystem, false, path);
  }

  private BlobstorePath createAbsolute(Path path){
    return path.isEmpty() ? getRoot() : new BlobstorePath(fileSystem, true, path);
  }

  Path simplePath() {
    return path;
  }

}
