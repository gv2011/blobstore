package com.github.gv2011.blobstore.fs;

import java.nio.channels.SeekableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.NotDirectoryException;
import java.nio.file.attribute.BasicFileAttributes;

import com.github.gv2011.util.icol.Opt;
import com.github.gv2011.util.icol.Path;

interface Engine {

  Opt<SeekableByteChannel> newByteChannel(Path simplePath);

  Opt<BasicFileAttributes> readAttributes(Path path);

  Opt<DirectoryStream<String>> newDirectoryStream(Path simplePath) throws NotDirectoryException;

}
