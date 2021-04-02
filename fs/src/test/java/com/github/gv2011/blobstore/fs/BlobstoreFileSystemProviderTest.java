package com.github.gv2011.blobstore.fs;

import static com.github.gv2011.util.icol.ICollections.mapOf;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import org.junit.Test;

import com.github.gv2011.util.icol.Opt;

public class BlobstoreFileSystemProviderTest {

  @Test
  public void test() throws IOException {
    //    System.out.println(StreamUtils.readText(()->Files.newInputStream(Paths.get("."))));
    
    final Function<com.github.gv2011.util.icol.Path,Opt<Path>> filePathMapper = this::mapPath;

    FileSystems.newFileSystem(URI.create("blobstore:/"), mapOf(BlobstoreFileSystemProvider.ENGINE, filePathMapper));
    final Path p = Paths.get(URI.create("blobstore:/a/b"));
    Files.newInputStream(p);
  }
  
  private Opt<Path> mapPath(com.github.gv2011.util.icol.Path path){
    return Opt.empty();    
  }

}
