package garden.bots.security;

public class FunkSecurityManager extends SecurityManager {

  @Override
  public void checkWrite(String file) {
    super.checkWrite(file);
    throw new SecurityException("😡 write is ⛔️");
  }

  @Override
  public void checkDelete(String file) {
    super.checkDelete(file);
    throw new SecurityException("😡 delete is ⛔️");
  }

  @Override
  public void checkExit(int status) {
    super.checkExit(status);
    throw new SecurityException("😡 exit is ⛔️");
  }

  @Override
  public void checkExec(String cmd) {
    super.checkExec(cmd);
    throw new SecurityException("😡 exec is ⛔️");
  }

  /*

  @Override
  public void checkRead(String file) {
    super.checkRead(file);
    throw new SecurityException("😡 read is ⛔️");
  }
  */

}
