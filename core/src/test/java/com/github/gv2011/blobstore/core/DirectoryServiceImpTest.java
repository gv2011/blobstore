package com.github.gv2011.blobstore.core;

import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.Verify.verifyEqual;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;
import org.slf4j.Logger;

import com.github.gv2011.blobstore.Blobstore;
import com.github.gv2011.blobstore.Directory;
import com.github.gv2011.util.FileUtils;
import com.github.gv2011.util.bytes.Bytes;
import com.github.gv2011.util.bytes.Hash256;
import com.github.gv2011.util.json.JsonFactory;
import com.github.gv2011.util.json.JsonUtils;

public class DirectoryServiceImpTest {
  
  private static final Logger LOG = getLogger(DirectoryServiceImpTest.class);
  
  private int i;

  @Test
  public void testReadLocalDirectory() throws IOException {
    final Path dir = createExampleDirectory();
    try{
      final JsonFactory jf = JsonUtils.jsonFactory();
      final DirectoryServiceImp directoryService = new DirectoryServiceImp(jf);
      final Directory dirObj = directoryService.readLocalDirectory(
        dir,
        this::callback
      );
      verifyEqual(
        directoryService.encode(dirObj),
            "{\n"
          + "  \"entries\": [\n"
          + "    {\n"
          + "      \"hash\": \"0Gddi/8A5N2B2QQqEaqc54/ERLEdbPnaNsgdhgLYXXQ=\",\n"
          + "      \"name\": \"Hello.txt\",\n"
          + "      \"size\": 5\n"
          + "    },\n"
          + "    {\n"
          + "      \"hash\": \"FQG85GmR9VDnqiiLO1O3BW+qr/Zgkn+Lx9oqUT0uJko=\",\n"
          + "      \"isDirectory\": true,\n"
          + "      \"name\": \"world-dir\",\n"
          + "      \"size\": 139\n"
          + "    },\n"
          + "    {\n"
          + "      \"hash\": \"Vj98LE/C23yMUdId1k9V1SovQUd3KxyrAK6f/V1lCG4=\",\n"
          + "      \"name\": \"zwei.txt\",\n"
          + "      \"size\": 4\n"
          + "    }\n"
          + "  ]\n"
          + "}"
      );
    }
    finally{
      FileUtils.delete(dir);
    }
  }
  
  private void callback(Path file, Bytes bytes){
    if(i==0) {
      verifyEqual(file.getFileName().toString(), "Hello.txt");
      verifyEqual(
        bytes.hash(), 
        Hash256.parse("D0 67 5D 8B FF 00 E4 DD 81 D9 04 2A 11 AA 9C E7 8F C4 44 B1 1D 6C F9 DA 36 C8 1D 86 02 D8 5D 74")
      );
    }
    else if(i==1){
      verifyEqual(file.getFileName().toString(), "World.txt");
      verifyEqual(
        bytes.hash(), 
        Hash256.parse("08 1A 1C 78 48 80 D9 D8 AC 38 EB 70 8C 2F 01 66 3D 97 C3 50 9E 79 18 1B BF E7 11 4A E8 37 6F 6B")
      );
    }
    else if(i==2){
      verifyEqual(file.getFileName().toString(), "world-dir");
      verifyEqual(
        bytes.hash(), 
        Hash256.parse("15 01 BC E4 69 91 F5 50 E7 AA 28 8B 3B 53 B7 05 6F AA AF F6 60 92 7F 8B C7 DA 2A 51 3D 2E 26 4A")
      );
    }
    else if(i==3){
      verifyEqual(file.getFileName().toString(), "zwei.txt");
      verifyEqual(
        bytes.hash(), 
        Hash256.parse("56 3F 7C 2C 4F C2 DB 7C 8C 51 D2 1D D6 4F 55 D5 2A 2F 41 47 77 2B 1C AB 00 AE 9F FD 5D 65 08 6E")
      );
    }
    else verify(false);
    i++;
  }
  
  @Test
  public void testStore() throws IOException {
    final Path dir = Files.createTempDirectory(DirectoryServiceImpTest.class.getName());
    LOG.info(dir.toAbsolutePath().toString());
    try {
      Path example = dir.resolve("example");
      createExampleDirectory(example);
      Blobstore blobstore = new BlobstoreImp(dir.resolve("store"));
      final JsonFactory jf = JsonUtils.jsonFactory();
      final DirectoryServiceImp directoryService = new DirectoryServiceImp(jf);
      directoryService.store(example, blobstore);
    } finally {
//      FileUtils.delete(dir);
    }
  }
  

  private Path createExampleDirectory() throws IOException {
    final Path dir = Files.createTempDirectory(DirectoryServiceImpTest.class.getName());
    createExampleDirectory(dir);
    return dir;
  }

  private void createExampleDirectory(Path dir) throws IOException {
    Files.createDirectories(dir);
    FileUtils.writeText("Guten", dir.resolve("Hello.txt"));
    FileUtils.writeText("Mojn", dir.resolve("zwei.txt"));
    final Path subDir = Files.createDirectory(dir.resolve("world-dir"));
    FileUtils.writeText("Tag!", subDir.resolve("World.txt"));
  }

  
}
