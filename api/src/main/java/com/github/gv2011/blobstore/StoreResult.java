package com.github.gv2011.blobstore;

import com.github.gv2011.util.beans.Bean;
import com.github.gv2011.util.bytes.Hash256;
import com.github.gv2011.util.icol.ISet;
import com.github.gv2011.util.icol.Opt;

public interface StoreResult extends Bean{
  
  static enum ResultCode{OK, WRONG_HASH, WRONG_SIZE, EXISTS}
  
  Opt<Hash256> hash();
  
  Opt<Long> size();
  
  ISet<ResultCode> resultCodes();
  
//  @Computed
//  default boolean success(){
//    return resultCodes().contains(ResultCode.OK);
//  }

}
