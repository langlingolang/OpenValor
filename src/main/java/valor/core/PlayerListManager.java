package valor.core;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class PlayerListManager {
   public ArrayList<String> names = new ArrayList<>();
   public File file;
   public Color color;

   public PlayerListManager(File file, Color color) {
      this.file = file;
      this.color = color;
   }

   public boolean contains(String name) {
      if (name == null) {
         return false;
      }
      for (String s : this.names) {
         if (s.equalsIgnoreCase(name)) {
            return true;
         }
      }
      return false;
   }

   public String add(String name) {
      if (name == null || this.contains(name)) {
         return null;
      }
      this.names.add(name);
      return name;
   }

   public String remove(String name) {
      if (name == null) {
         return null;
      }
      for (int i = 0; i < this.names.size(); i++) {
         String s = this.names.get(i);
         if (s.equalsIgnoreCase(name)) {
            this.names.remove(i);
            return s;
         }
      }
      return null;
   }

   public void clear() {
      this.names.clear();
   }

   public void load() {
      if (this.file == null || !this.file.exists()) {
         return;
      }
      this.names.clear();
      try (BufferedReader br = new BufferedReader(new FileReader(this.file))) {
         String line;
         while ((line = br.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty()) {
               this.names.add(line);
            }
         }
      } catch (Exception ignored) {
      }
   }

   public void save() {
      if (this.file == null) {
         return;
      }
      try {
         File parent = this.file.getParentFile();
         if (parent != null && !parent.exists()) {
            parent.mkdirs();
         }
         try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.file))) {
            for (String s : this.names) {
               bw.write(s);
               bw.newLine();
            }
         }
      } catch (Exception ignored) {
      }
   }
}
