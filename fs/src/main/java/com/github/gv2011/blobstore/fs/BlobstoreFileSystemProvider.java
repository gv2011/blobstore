package com.github.gv2011.blobstore.fs;

import static com.github.gv2011.util.CollectionUtils.get;
import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.format;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.github.gv2011.blobstore.BlobFactory;
import com.github.gv2011.util.CachedConstant;
import com.github.gv2011.util.CollectionUtils;
import com.github.gv2011.util.Constants;

public final class BlobstoreFileSystemProvider extends FileSystemProvider {
  
  static final String ENGINE = "engine";
  
  private final BlobstoreFileStore blobstoreFileStore;
  private final CachedConstant<BlobstoreFs> fileSystem = Constants.cachedConstant();
  
  public BlobstoreFileSystemProvider(){
    blobstoreFileStore = new BlobstoreFileStore();
  }

  @Override
  public String getScheme() {
    return BlobFactory.SCHEME;
  }

  @Override
  public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
    verify(uri, u->u.getScheme().equalsIgnoreCase(BlobFactory.SCHEME));
    fileSystem.set(new BlobstoreFs(this, blobstoreFileStore, (Engine)get(env, ENGINE)));
    return fileSystem.get();
  }

  @Override
  public FileSystem getFileSystem(URI uri) {
    verify(uri, u->u.getScheme().equalsIgnoreCase(BlobFactory.SCHEME));
    return fileSystem.get();
  }

  @Override
  public Path getPath(URI uri) {
    verify(uri, u->u.getScheme().equalsIgnoreCase(BlobFactory.SCHEME));
    return new BlobstorePath(fileSystem.get(), uri);
  }

  @Override
  public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
    return engine().newByteChannel(simplePath(path))
      .orElseThrow(()->new FileNotFoundException(format("File {} not found.", path)))
    ;
  }

  @Override
  public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
    return engine().newDirectoryStream(simplePath(dir))
      .map(ds->new DirectoryStream<Path>(){
        @Override
        public void close() throws IOException {ds.close();}
        @Override
        public Iterator<Path> iterator() {
          return CollectionUtils.mapIterator(ds.iterator(), dir::resolve);
        }
      })
      .orElseThrow(()->new FileNotFoundException(format("File {} not found.", dir)))
    ;
  }

  @Override
  public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
    throw new AccessDeniedException("This is a read only file system.");
  }

  @Override
  public void delete(Path path) throws IOException {
    throw new AccessDeniedException("This is a read only file system.");
  }

  @Override
  public void copy(Path source, Path target, CopyOption... options) throws IOException {
    throw new AccessDeniedException("This is a read only file system.");
  }

  @Override
  public void move(Path source, Path target, CopyOption... options) throws IOException {
    throw new AccessDeniedException("This is a read only file system.");
  }

  @Override
  public boolean isSameFile(Path path1, Path path2) throws IOException {
    return path1.equals(path2);
  }

  @Override
  public boolean isHidden(Path path) throws IOException {
    return false;
  }

  @Override
  public FileStore getFileStore(Path path) throws IOException {
    final BlobstoreFileStore blobstoreFileStore = new BlobstoreFileStore();
    return blobstoreFileStore;
  }

  @Override
  public void checkAccess(Path path, AccessMode... modes) throws IOException {
    if(Arrays.stream(modes).anyMatch(m->m==AccessMode.EXECUTE || m==AccessMode.WRITE)){
      throw new AccessDeniedException("WRITE and EXECUTE access not supported.");
    }
    else readAttributes(path);
  }

  @Override
  public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
    if(BasicFileAttributeView.class!=type) return null;
    else return type.cast(new BasicFileAttributeView(){
      @Override
      public String name() {return "basic";}
      @Override
      public BasicFileAttributes readAttributes() throws IOException {
        return BlobstoreFileSystemProvider.this.readAttributes(path);
      }
      @Override
      public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
        throw new AccessDeniedException("This is a read only file system.");
      }
    });
  }

  @Override
  public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
    if(BasicFileAttributes.class!=type) throw new UnsupportedOperationException();
    return type.cast(readAttributes(path));
  }

  public BasicFileAttributes readAttributes(Path path) throws IOException {
    return engine().readAttributes(simplePath(path)).orElseThrow(()->new FileNotFoundException());
  }

  @Override
  public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
    throw new AccessDeniedException("This is a read only file system.");
  }
  
  private Engine engine() {
    return fileSystem.get().engine();
  }
  
  private com.github.gv2011.util.icol.Path simplePath(Path path){
    return ((BlobstorePath)path).simplePath();
  }

}
