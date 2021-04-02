package com.github.gv2011.blobstore.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.github.gv2011.blobstore.StoreResult;
import com.github.gv2011.util.FileUtils;
import com.github.gv2011.util.bytes.ByteUtils;

public class BlobstoreImpTest {

  @Test
  public void test() throws IOException{
    final Path dir = Files.createTempDirectory(BlobstoreImpTest.class.getName());
    try{
      final BlobstoreImp store = new BlobstoreImp(dir);
      final StoreResult r = store.store(ByteUtils.asUtf8("Hallo").content().openStream());
      assertThat(
        r.hash().get().content().toString(), 
        is("75 36 92 EC 36 AD B4 C7 94 C9 73 94 5E B2 A9 9C 16 49 70 3E A6 F7 6B F2 59 AB B4 FB 83 8E 01 3E")
      );
    }
    finally{
      FileUtils.delete(dir);
    }
  }

}
