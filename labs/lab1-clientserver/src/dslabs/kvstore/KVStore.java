package dslabs.kvstore;

import java.util.HashMap;
import java.util.Map;

import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Result;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class KVStore implements Application {

  public interface KVStoreCommand extends Command {}

  public interface SingleKeyCommand extends KVStoreCommand {
    String key();
  }

  @Data
  public static final class Get implements SingleKeyCommand {
    @NonNull private final String key;

    @Override
    public boolean readOnly() {
      return true;
    }
  }

  @Data
  public static final class Put implements SingleKeyCommand {
    @NonNull private final String key, value;
  }

  @Data
  public static final class Append implements SingleKeyCommand {
    @NonNull private final String key, value;
  }

  public interface KVStoreResult extends Result {}

  @Data
  public static final class GetResult implements KVStoreResult {
    @NonNull private final String value;
  }

  @Data
  public static final class KeyNotFound implements KVStoreResult {}

  @Data
  public static final class PutOk implements KVStoreResult {}

  @Data
  public static final class AppendResult implements KVStoreResult {
    @NonNull private final String value;
  }

  private Map<String, String> _data = new HashMap<String, String>();

  @Override
  public KVStoreResult execute(Command command) {
    if (command instanceof Get) {
      Get g = (Get) command;

      if (_data.containsKey(g.key)) {
        return new GetResult(_data.get(g.key));
      }
      
      return new KeyNotFound();

    }

    if (command instanceof Put) {
      Put p = (Put) command;
      _data.put(p.key, p.value);
      return new PutOk();
    }

    if (command instanceof Append) {
      Append a = (Append) command;
      String newString = _data.getOrDefault(a.key,"") + a.value;
      _data.put(a.key, newString);
      return new AppendResult(newString);
    }

    throw new IllegalArgumentException();
  }
}
