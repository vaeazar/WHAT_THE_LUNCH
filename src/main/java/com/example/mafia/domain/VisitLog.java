package com.example.mafia.domain;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
public class VisitLog implements Serializable {
  int dayOfWeek;
  String visitTeam;
  Store storeInfo;
  String regDttm;
}
