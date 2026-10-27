package valor.command;

import java.util.ArrayList;

public abstract class Command {
   public final ArrayList<String> aliases;

   public abstract void execute(ArrayList<String> args);

   public Command(ArrayList<String> aliases) {
      this.aliases = aliases;
   }
}
