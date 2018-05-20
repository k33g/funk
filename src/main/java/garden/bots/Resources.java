package garden.bots;

public class Resources {
    private static ClassLoader classLoader = Main.class.getClassLoader();

    public static Class<?>[] getList() throws ClassNotFoundException {
        Class <?> resources[] = {
                classLoader.loadClass("garden.bots.resources.StaticResource"),
                classLoader.loadClass("garden.bots.resources.JSFunctionsResource"),
                classLoader.loadClass("garden.bots.resources.KTFunctionsResource")
        };

        return resources;
    }
}