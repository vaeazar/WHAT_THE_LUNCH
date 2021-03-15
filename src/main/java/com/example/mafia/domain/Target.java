package com.example.mafia.domain;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class Target implements Serializable {
  String roomId;
  String roomName;
  String weekExcept;
  List<String> nationAndKinds;
  List<String> kinds;

  public Target() {
    this.weekExcept = "N";
  }

  public Target(String weekExcept) {
    this.weekExcept = weekExcept;
  }
}
