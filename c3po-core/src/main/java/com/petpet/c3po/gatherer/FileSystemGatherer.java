package com.petpet.c3po.gatherer;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.common.Constants;

public class FileSystemGatherer {

  private static final Logger LOG = LoggerFactory.getLogger(FileSystemGatherer.class);

  private Map<String, Object> config;

  private List<String> files;

  private long count;

  private int pointer;

  public FileSystemGatherer(Map<String, Object> config) {
    this.config = config;
    this.init();
  }

  public void setConfig(Map<String, Object> config) {
    this.config = config;

  }

  public long getCount() {
    return this.count;
  }

  private long count(File dir, FileFilter filter) {
    long sum = 0;

    if (dir.isDirectory()) {
      File[] files = dir.listFiles(filter);
      for (File f : files) {
        sum += count(f, filter);
      }
    } else {
      return ++sum;
    }

    return sum;
  }

  public List<String> getNext(int count) {
    List<String> next = new ArrayList<String>();

    if (count <= 0) {
      return next;
    }

    while (pointer < files.size() && count > 0) {
      // try {
      next.add(this.files.get(pointer++));
      count--;
      // } catch (FileNotFoundException e) {
      // e.printStackTrace();
      // }
    }

    return next;
  }

  public List<String> getAll() {
    List<String> all = new ArrayList<String>();
    for (String path : this.files) {
      // try {
      all.add(path);
      // } catch (FileNotFoundException e) {
      // e.printStackTrace();
      // }
    }
    return all;
  }

  // TODO fix constants.
  private void init() {
    this.files = new ArrayList<String>();
    this.pointer = 0;
    String path = (String) this.config.get(Constants.CNF_COLLECTION_LOCATION);
    boolean recursive = (Boolean) this.config.get(Constants.CNF_RECURSIVE);

    if (path == null) {
      LOG.error("No path config provided");
      return;
    }

    File dir = new File(path);

    if (!dir.exists() || !dir.isDirectory()) {
      LOG.error("Directory '{}' does not exist, or is not a directory", path);
      return;
    }

    final XMLFileFilter filter = new XMLFileFilter(recursive);
    this.traverseFiles(dir, filter);
    this.count = this.count(dir, filter);

  }

  private void traverseFiles(File file, FileFilter filter) {
    if (file.isDirectory()) {
      File[] files = file.listFiles(filter);
      for (File f : files) {
        traverseFiles(f, filter);
      }
    } else {
      this.files.add(file.getAbsolutePath());
    }
  }

  private class XMLFileFilter implements FileFilter {

    private boolean recursive;

    public XMLFileFilter(boolean recursive) {
      this.recursive = recursive;
    }

    @Override
    public boolean accept(File pathname) {
      boolean accept = false;

      if ((pathname.isDirectory() && this.recursive) || pathname.getName().endsWith(".xml"))
        accept = true;

      return accept;
    }

  }

}