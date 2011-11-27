package nz.ac.vuw.ecs.rprofs.server.weaving;

import nz.ac.vuw.ecs.rprofs.domain.MethodUtils;
import nz.ac.vuw.ecs.rprofs.server.domain.Method;
import nz.ac.vuw.ecs.rprofs.server.domain.id.ClazzId;
import nz.ac.vuw.ecs.rprofs.server.domain.id.MethodId;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Author: Stephen Nelson <stephen@sfnelson.org>
 * Date: 15/09/11
 */
public class MethodUtilsTest {

	private ClazzId owner = new ClazzId(2l);
	private MethodId id = new MethodId(1l);

	@Test
	public void testIsNative() throws Exception {
		assertTrue(MethodUtils.isNative(
				new Method(id, 0, "foobar", owner, "foo.Bar", "()V", Opcodes.ACC_NATIVE | Opcodes.ACC_DEPRECATED)));
		assertFalse(MethodUtils.isNative(
				new Method(id, 0, "foobar", owner, "foo.Bar", "()V", Opcodes.ACC_PUBLIC)));
	}

	@Test
	public void testIsMain() throws Exception {
		assertTrue(MethodUtils.isMain(
				new Method(id, 0, "main", owner, "foo.Bar", "([Ljava/lang/String;)V",
						Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)));
		assertFalse(MethodUtils.isMain(
				new Method(id, 0, "main", owner, "foo.Bar", "(I[Ljava/lang/String;)V",
						Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)));
		assertFalse(MethodUtils.isMain(
				new Method(id, 0, "main", owner, "foo.Bar", "(I[Ljava/lang/String;)V",
						Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_NATIVE)));
		assertFalse(MethodUtils.isMain(
				new Method(id, 0, "main", owner, "foo.Bar", "([Ljava/lang/String;)V",
						Opcodes.ACC_PUBLIC)));
		assertFalse(MethodUtils.isMain(
				new Method(id, 0, "man", owner, "foo.Bar", "([Ljava/lang/String;)V",
						Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)));

	}

	@Test
	public void testIsInit() throws Exception {
		assertTrue(MethodUtils.isInit(
				new Method(id, 0, "<init>", owner, "foo.Bar", "([Ljava/lang/String;)V",
						Opcodes.ACC_PUBLIC)));
		assertFalse(MethodUtils.isInit(
				new Method(id, 0, "main", owner, "foo.Bar", "([Ljava/lang/String;)V",
						Opcodes.ACC_PUBLIC)));
	}

	@Test
	public void testIsCLInit() throws Exception {
		assertTrue(MethodUtils.isCLInit(
				new Method(id, 0, "<clinit>", owner, "foo.Bar", "()V",
						Opcodes.ACC_STATIC)));
		assertFalse(MethodUtils.isCLInit(
				new Method(id, 0, "main", owner, "foo.Bar", "([Ljava/lang/String;)V",
						Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)));
	}

	@Test
	public void testIsEquals() throws Exception {
		assertTrue(MethodUtils.isEquals(
				new Method(id, 0, "equals", owner, "foo.Bar", "(Ljava/lang/Object;)Z",
						Opcodes.ACC_PUBLIC)));
		assertFalse(MethodUtils.isEquals(
				new Method(id, 0, "equals", owner, "foo.Bar", "(Ljava/lang/Object;)V",
						Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)));
		assertFalse(MethodUtils.isEquals(
				new Method(id, 0, "equals", owner, "foo.Bar", "(Ljava/lang/Object;)V",
						Opcodes.ACC_PRIVATE)));
		assertFalse(MethodUtils.isEquals(
				new Method(id, 0, "equals", owner, "foo.Bar", "()V",
						Opcodes.ACC_PUBLIC)));
	}

	@Test
	public void testIsHashCode() throws Exception {
		assertTrue(MethodUtils.isHashCode(
				new Method(id, 0, "hashCode", owner, "foo.Bar", "()I",
						Opcodes.ACC_PUBLIC)));
		assertFalse(MethodUtils.isHashCode(
				new Method(id, 0, "hashCode", owner, "foo.Bar", "()I",
						Opcodes.ACC_PRIVATE)));
		assertFalse(MethodUtils.isHashCode(
				new Method(id, 0, "hashCode", owner, "foo.Bar", "()Z",
						Opcodes.ACC_PUBLIC)));
		assertFalse(MethodUtils.isHashCode(
				new Method(id, 0, "hashcode", owner, "foo.Bar", "()I",
						Opcodes.ACC_PUBLIC)));
	}

	@Test
	public void testIsStatic() throws Exception {
		assertTrue(MethodUtils.isStatic(
				new Method(id, 0, "hashCode", owner, "foo.Bar", "()I",
						Opcodes.ACC_STATIC | Opcodes.ACC_PROTECTED)));
		assertFalse(MethodUtils.isStatic(
				new Method(id, 0, "hashCode", owner, "foo.Bar", "()I",
						0)));
	}
}
