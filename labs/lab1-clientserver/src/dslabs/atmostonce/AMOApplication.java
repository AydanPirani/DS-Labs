package dslabs.atmostonce;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import javax.swing.undo.CannotRedoException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Result;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public final class AMOApplication<T extends Application> implements Application {
  @Getter @NonNull private final T application;

  // Your code here...
  private HashMap<Address, Integer> lastExecuted = new HashMap<>();
  private HashMap<Address, Result> currentResults = new HashMap<>();

  @Override
  public AMOResult execute(Command command) {
    if (!(command instanceof AMOCommand)) {
      throw new IllegalArgumentException();
    }

      AMOCommand amoCommand = (AMOCommand) command;

      Address sender = amoCommand.sender();
      int currentSequenceNum = amoCommand.sequenceNum();
      int lastSequenceNum = lastExecuted.getOrDefault(sender, -1);
  
      // Execute
      if (currentSequenceNum > lastSequenceNum) {
        lastExecuted.put(sender, currentSequenceNum);
        Result result = application.execute(amoCommand.command());
        currentResults.put(sender, result);
        return new AMOResult(result, currentSequenceNum);
      }
      
      if (currentSequenceNum == lastSequenceNum) {
        Result result = currentResults.get(sender);
        return new AMOResult(result, currentSequenceNum);
      }
  
      return new AMOResult(null, 0);
  }

  public Result executeReadOnly(Command command) {
    if (!command.readOnly()) {
      throw new IllegalArgumentException();
    }

    if (command instanceof AMOCommand) {
      return execute(command);
    }

    return application.execute(command);
  }

  public boolean alreadyExecuted(AMOCommand amoCommand) {
    Address sender = amoCommand.sender();
    int currentSequenceNum = amoCommand.sequenceNum();
    int lastSequenceNum = lastExecuted.getOrDefault(sender, 1);
    
    return currentSequenceNum <= lastSequenceNum;
  }
}
