package com.example.mafia;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MafiaApplicationTests {

  @Test
  void contextLoads() {
  }

  @Test
  void ant() {
    int temp = 10;
    int count = 0;
    int idx = 0;
    String resultString = "";
    List<Integer> tempList = new ArrayList<>();
    List<Integer> result = new ArrayList<>();
    result.add(1);

    for (int i = 0; i < 10; i++) {
      tempList = new ArrayList<>();
      for (int element : result) {
        idx++;
        if (temp == 10) {
          temp = element;
          count++;
        } else if (temp != element) {
          tempList.add(count);
          tempList.add(temp);
          temp = element;
          count = 1;
        } else {
          count++;
        }
        if (idx == result.size()) {
          tempList.add(count);
          tempList.add(temp);
          temp = 10;
          count = 0;
          idx = 0;
        }
      }
      result = tempList;
      resultString = "";
      for (int element : result) {
        resultString += element;
      }
      System.out.println("result : " + resultString);
    }
  }
}
