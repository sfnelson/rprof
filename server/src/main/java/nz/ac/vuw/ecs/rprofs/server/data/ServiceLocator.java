package nz.ac.vuw.ecs.rprofs.server.data;


public class ServiceLocator implements com.google.gwt.requestfactory.shared.ServiceLocator {

	@Override
	public Object getInstance(Class<?> clazz) {
		System.out.print("[locator] requested " + clazz + ": ");
		try {
			Object o = clazz.newInstance();
			System.out.println(o);
			return o;
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
