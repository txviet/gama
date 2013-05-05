package msi.gama.jogl.scene;

import static javax.media.opengl.GL.GL_COMPILE;
import java.util.*;
import msi.gama.jogl.utils.JOGLAWTGLRenderer;

public class SceneObjects<T extends AbstractObject> implements Iterable<T> {

	public static class Static<T extends AbstractObject> extends SceneObjects<T> {

		Static(ObjectDrawer<T> drawer, boolean asList) {
			super(drawer, asList);
		}

		@Override
		public void add(T object) {
			if ( openGLListIndex != null ) { return; }
			super.add(object);
		}

		@Override
		public void clear(JOGLAWTGLRenderer renderer) {}
	}

	final ObjectDrawer<T> drawer;
	final List<T> objects = new ArrayList();
	Integer openGLListIndex;
	final boolean drawAsList;

	SceneObjects(ObjectDrawer<T> drawer, boolean asList) {
		this.drawer = drawer;
		drawAsList = asList;
	}

	@Override
	public Iterator<T> iterator() {
		return objects.iterator();
	}

	protected void clearObjects() {
		objects.clear();
	}

	public void clear(JOGLAWTGLRenderer renderer) {
		clearObjects();
		if ( openGLListIndex != null ) {
			renderer.gl.glDeleteLists(openGLListIndex, 1);
			openGLListIndex = null;
		}
	}

	public Integer getIndexInOpenGLList() {
		return openGLListIndex;
	}

	public void setIndexInOpenGLList(Integer index) {
		this.openGLListIndex = index;
	}

	public List<T> getObjects() {
		return objects;
	}

	public void add(T object) {
		objects.add(object);
	}

	public void draw(boolean picking) {
		if ( picking ) {
			drawer.gl.glPushMatrix();
			drawer.gl.glInitNames();
			drawer.gl.glPushName(0);
			for ( T object : objects ) {
				object.draw(drawer, picking);
			}
			drawer.gl.glPopName();
			drawer.gl.glPopMatrix();
		} else if ( drawAsList ) {
			if ( openGLListIndex == null ) {
				openGLListIndex = drawer.gl.glGenLists(1);
				drawer.gl.glNewList(openGLListIndex, GL_COMPILE);
				for ( T object : objects ) {
					object.draw(drawer, picking);
				}
				drawer.gl.glEndList();
			}
			drawer.gl.glCallList(openGLListIndex);
		} else {
			for ( T object : objects ) {
				object.draw(drawer, picking);
			}
		}
	}

	public void dispose() {
		drawer.dispose();
		// if ( openGLListIndex != null ) {
		// drawer.gl.glDeleteLists(openGLListIndex, 1);
		// }
	}

}