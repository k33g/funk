package garden.bots.resources;

import net.redpipe.engine.resteasy.FileResource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/{path:(.*)?}")
public class StaticResource extends FileResource {
  @GET
  public Response index(@PathParam("path") String path) throws IOException {
    return super.getFile(path.equals("") ? "index.html" : path);
  }
}
