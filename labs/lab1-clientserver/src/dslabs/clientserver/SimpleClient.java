package dslabs.clientserver;

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
  }

  /* -----------------------------------------------------------------------------------------------
   *  Client Methods
   * ---------------------------------------------------------------------------------------------*/
  @Override
  public synchronized void sendCommand(Command command) {
    globalResult = null;

    Request currRequest = new Request(command, globalSequenceNum);
    globalSequenceNum++;
    globalRequest = currRequest;

    send(globalRequest, serverAddress);
    set(new ClientTimer(), 10000);
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
    if (m.sequenceNum() == globalRequest.sequenceNum()) {
      globalRequest = null;
      globalResult = m.result();
    }
  }

  /* -----------------------------------------------------------------------------------------------
   *  Timer Handlers
   * ---------------------------------------------------------------------------------------------*/
  private synchronized void onClientTimer(ClientTimer t) {
    send(globalRequest, serverAddress);
    set(new ClientTimer(), ClientTimer.CLIENT_RETRY_MILLIS);
  }
}
