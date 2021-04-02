module com.github.gv2011.blobstore.fs{
  requires transitive com.github.gv2011.blobstore;
  requires transitive com.github.gv2011.util;
  provides java.nio.file.spi.FileSystemProvider with com.github.gv2011.blobstore.fs.BlobstoreFileSystemProvider;
}