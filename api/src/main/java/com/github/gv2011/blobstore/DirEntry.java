package com.github.gv2011.blobstore;

import java.time.Instant;

import com.github.gv2011.util.beans.Bean;
import com.github.gv2011.util.bytes.Bytes;
import com.github.gv2011.util.icol.Opt;

public interface DirEntry extends Bean{
  
  String name();
  
  Boolean isDirectory();
  
  Bytes hash();
  
  Long size();

  Opt<Instant> modified();

}
