package nz.ac.vuw.ecs.rprofs.server.request;

import java.util.List;

import nz.ac.vuw.ecs.rprofs.server.domain.Class;

public interface ClassRequest {

	List<? extends Class> findClasses(String dataset, String pkg);

}
