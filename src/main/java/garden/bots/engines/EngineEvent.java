package garden.bots.engines;

import garden.bots.resources.FunctionPayload;

import java.util.Date;

public class EngineEvent {
  public String action;
  public String status;
  public long elapsedTime;
  public long startTime;
  public Date when;

  public String functionName;
  public String functionKind;

  public Object parameters;
  public Object result;

  public EngineEvent(String status, FunctionPayload funktion, Object result) {
    this.status = status;
    this.functionName = funktion.name;
    this.functionKind = funktion.kind;
    this.parameters = funktion.parameters;
    this.result = result;
  }
}
