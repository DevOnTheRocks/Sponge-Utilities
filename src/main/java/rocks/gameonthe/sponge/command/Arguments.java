package rocks.gameonthe.sponge.command;

import com.google.common.collect.ImmutableList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.annotation.Nullable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

public class Arguments {

  public static CommandElement dateTime(Text key) {
    return new DateTimeElement(key, false);
  }

  private static class DateTimeElement extends CommandElement {

    private final boolean returnNow;

    protected DateTimeElement(Text key, boolean returnNow) {
      super(key);
      this.returnNow = returnNow;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args)
        throws ArgumentParseException {
      if (!args.hasNext() && this.returnNow) {
        return LocalDateTime.now();
      }
      Object state = args.getState();
      String date = args.next();
      try {
        return LocalDateTime.parse(date);
      } catch (DateTimeParseException ex) {
        try {
          return LocalDateTime.of(LocalDate.now(), LocalTime.parse(date));
        } catch (DateTimeParseException ex2) {
          try {
            return LocalDateTime.of(LocalDate.parse(date), LocalTime.MIDNIGHT);
          } catch (DateTimeParseException ex3) {
            if (this.returnNow) {
              args.setState(state);
              return LocalDateTime.now();
            }
            throw args.createError(Text.of("Invalid date-time!"));
          }
        }
      }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
      String date = LocalDateTime.now().withNano(0).toString();
      if (date.startsWith(args.nextIfPresent().orElse(""))) {
        return ImmutableList.of(date);
      } else {
        return ImmutableList.of();
      }
    }

    @Override
    public Text getUsage(CommandSource src) {
      if (!this.returnNow) {
        return super.getUsage(src);
      } else {
        return Text.of("[", this.getKey(), "]");
      }
    }
  }
}
