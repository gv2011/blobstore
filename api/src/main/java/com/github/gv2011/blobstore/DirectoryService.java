package com.github.gv2011.blobstore;

import java.nio.file.Path;
import java.util.function.BiConsumer;

import com.github.gv2011.util.bytes.Bytes;
import com.github.gv2011.util.bytes.Hash256;

public interface DirectoryService {

    Directory readLocalDirectory(Path directory, BiConsumer<Path,Bytes> listener);
  
    Hash256 store(Path fileOrDirectory, Blobstore store);
  
}
