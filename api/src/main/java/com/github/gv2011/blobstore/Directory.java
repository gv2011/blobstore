package com.github.gv2011.blobstore;

import com.github.gv2011.util.beans.Bean;
import com.github.gv2011.util.icol.IList;
import com.github.gv2011.util.icol.Opt;

public interface Directory extends Bean{
  
  IList<DirEntry> entries();
  
  default Opt<DirEntry> entry(String name){
    return entries().stream().filter(e->e.name().equals(name)).tryFindAny();
  }

}
