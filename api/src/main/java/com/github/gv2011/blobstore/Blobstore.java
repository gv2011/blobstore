package com.github.gv2011.blobstore;

import java.io.InputStream;

import com.github.gv2011.util.XStream;
import com.github.gv2011.util.bytes.Hash256;
import com.github.gv2011.util.bytes.HashAndSize;
import com.github.gv2011.util.icol.Opt;

public interface Blobstore {
  
  StoreResult store(InputStream data);
  
  StoreResult store(InputStream data, Opt<Hash256> hash, Opt<Long> size);
  
  Opt<InputStream> tryGet(Hash256 hash);
  
  Opt<Long> tryGetSize(Hash256 hash);
  
  XStream<HashAndSize> list(Hash256 startInclusive);
  
  boolean remove(Hash256 hash);
  

}
