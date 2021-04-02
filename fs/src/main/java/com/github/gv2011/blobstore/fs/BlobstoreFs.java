package com.github.gv2011.blobstore.fs;

import static com.github.gv2011.util.icol.ICollections.toPath;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import com.github.gv2011.util.icol.ISet;
import com.github.gv2011.util.icol.Opt;

final class BlobstoreFs extends FileSystem{

  private final FileSystemProvider provider;
  private final BlobstoreFileStore fileStore;
  private final Engine engine;
  
  final BlobstorePath rootPath = new BlobstorePath(this);

  BlobstoreFs(
    BlobstoreFileSystemProvider provider, 
    BlobstoreFileStore fileStore, 
    Engine engine
  ) {
    this.provider = provider;
    this.fileStore = fileStore;
    this.engine = engine;
  }

  @Override
  public FileSystemProvider provider() {
    return provider ;
  }

  @Override
  public void close() throws IOException {
  }

  @Override
  public boolean isOpen() {
    return true;
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public String getSeparator() {
    return "/";
  }

  @Override
  public ISet<Path> getRootDirectories() {
    return Opt.of(rootPath);
  }

  @Override
  public ISet<FileStore> getFileStores() {
    return Opt.of(fileStore);
  }

  @Override
  public Set<String> supportedFileAttributeViews() {
    return Opt.of("Basic");
  }

  @Override
  public Path getPath(String first, String... more) {
    return new BlobstorePath(this, true, Stream.concat(Stream.of(first), Arrays.stream(more)).collect(toPath()));
  }

  @Override
  public PathMatcher getPathMatcher(String syntaxAndPattern) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UserPrincipalLookupService getUserPrincipalLookupService() {
    throw new UnsupportedOperationException();
  }

  @Override
  public WatchService newWatchService() throws IOException {
    throw new UnsupportedOperationException();
  }

  Engine engine(){
    return engine;
  }

}
