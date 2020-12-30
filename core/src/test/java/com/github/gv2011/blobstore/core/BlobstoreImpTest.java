package com.github.gv2011.blobstore.core;

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
    final BlobstoreImp store = new BlobstoreImp(dir);
    final StoreResult r = store.store(ByteUtils.asUtf8("Hallo").content().openStream());
    //assertThat(r.hash().toString(), is(""));
    FileUtils.delete(dir);
  }

}
