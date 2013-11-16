package framework;

import java.util.List;

import com.google.common.util.concurrent.Service;

public interface ServiceLoader {
	public List<Service> getAllServices();
}
