package dslabs.clientserver;

import org.checkerframework.checker.units.qual.A;

import dslabs.atmostonce.AMOCommand;
import dslabs.atmostonce.AMOResult;
import dslabs.framework.Address;
import dslabs.framework.Client;
import dslabs.framework.Command;
import dslabs.framework.Node;
import dslabs.framework.Result;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Simple client that sends requests to a single server and returns responses.
 *
 * <p>See the documentation of {@link Client} and {@link Node} for important implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleClient extends Node implements Client {
  private final Address serverAddress;

  // Your code here...
  private Request globalRequest = null;
  private Result globalResult = null;
  private int globalSequenceNum = 0;

  /* -----------------------------------------------------------------------------------------------
   *  Construction and Initialization
   * ---------------------------------------------------------------------------------------------*/
  public SimpleClient(Address address, Address serverAddress) {
    super(address);
    this.serverAddress = serverAddress;
  }

  @Override
  public synchronized void init() {
    // No initialization necessary
    set(new ClientTimer(), ClientTimer.CLIENT_RETRY_MILLIS);
  }

  /* -----------------------------------------------------------------------------------------------
   *  Client Methods
   * ---------------------------------------------------------------------------------------------*/
  @Override
  public synchronized void sendCommand(Command command) {
    globalResult = null;
    
    AMOCommand amoCommand = new AMOCommand(command, globalSequenceNum, address());
    globalRequest = new Request(amoCommand);;

    send(globalRequest, serverAddress);
  }

  @Override
  public synchronized boolean hasResult() {
    return globalResult != null;
  }

  @Override
  public synchronized Result getResult() throws InterruptedException {
    if (globalResult == null) {
      wait();
    }
    
    return globalResult;
  }

  /* -----------------------------------------------------------------------------------------------
   *  Message Handlers
   * ---------------------------------------------------------------------------------------------*/
  private synchronized void handleReply(Reply m, Address sender) {
    AMOResult result = m.result();

    if (globalRequest != null && result.sequenceNum() == globalSequenceNum) {
      globalRequest = null;
      globalResult = m.result().result();
      globalSequenceNum++;
      notify();
    }
  }

  /* -----------------------------------------------------------------------------------------------
   *  Timer Handlers
   * ---------------------------------------------------------------------------------------------*/
  private synchronized void onClientTimer(ClientTimer t) {
    if (globalRequest != null) {
      send(globalRequest, serverAddress);
    }

    set(new ClientTimer(), ClientTimer.CLIENT_RETRY_MILLIS);
  }
}
