package org.nohope.app.spring;

import org.junit.Test;
import org.nohope.app.spring.module.IModule;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 7/27/12 5:29 PM
 */
public class SpringAsyncModularAppTest {

    @Test
    public void appIsAnonymousClass() throws Exception {
        final AppWithContainer app = new AppWithContainer(null, "", "/") {
        };
        assertEquals("springAsyncModularAppTest", app.getAppName());
        assertEquals("/", app.getAppMetaInfNamespace());
        assertEquals("/", app.getModuleMetaInfNamespace());
    }

    @Test
    public void appDefaultContextOverriding() throws Exception {
        final AppWithContainer app = new AppWithContainer("appo", "appContextOverriding");
        probe(app);

        assertNotNull(app.getContext());
        assertEquals("appBeanOverridden", app.getContext().getBean("appBean"));
    }

    @Test
    public void moduleDefaultContextOverriding() throws Exception {
        final AppWithContainer app = new AppWithContainer("app", "", "moduleContextOverriding");
        probe(app);

        assertEquals(1, app.getModules().size());
        final InjectModuleWithContextValue m = getModule(app, 0);
        assertEquals("overridden", m.getValue());
        assertEquals("moduleo", m.getName());
    }

    @Test
    public void searchPathsDetermining() throws Exception {
        final AppWithContainer app = new AppWithContainer();
        assertEquals("appWithContainer", app.getAppName());
        assertEquals("sev/omnitrack/app/spring/", app.getAppMetaInfNamespace());
        assertEquals("sev/omnitrack/app/spring/module/", app.getModuleMetaInfNamespace());
        assertEquals(IModule.class, app.getTargetModuleClass());
    }

    @Test
    public void illegalModuleDescriptor() throws Exception {
        final AppWithContainer app = new AppWithContainer("app", "", "illegalDescriptor") {
        };
        probe(app);
        assertEquals(0, app.getModules().size());
    }

    @Test
    public void nonexistentModuleClass() throws Exception {
        final AppWithContainer app = new AppWithContainer("app", "", "nonexistentClass") {
        };
        probe(app);

        assertEquals(0, app.getModules().size());
    }

    @Test
    public void notAModuleClass() throws Exception {
        final AppWithContainer app = new AppWithContainer("app", "", "notAModule") {
        };
        probe(app);

        assertEquals(0, app.getModules().size());
    }

    @Test
    public void legalModuleDefaultContext() throws Exception {
        final AppWithContainer app = new AppWithContainer("app", "", "legalModuleDefaultContext") {
        };
        probe(app);

        assertEquals(1, app.getModules().size());
        final InjectModuleWithContextValue m = getModule(app, 0);
        assertEquals("123", m.getValue());
        assertEquals("legal", m.getName());
        final Properties p = m.getProperties();
        assertEquals(2, p.size());
        assertEquals("\"fuck yeah!\"", p.getProperty("property"));

        // check for app beans inheritance
        assertEquals("appBean", m.getContext().getBean("appBean"));
    }

    @SuppressWarnings("unchecked")
    private static <T extends IModule> T getModule(final AppWithContainer app,
                                                   final int index) {
        assertTrue(app.getModules().size() >= index);
        final IModule module = app.getModules().get(index);
        assertNotNull(module);
        try {
            return (T) module;
        } catch (ClassCastException e) {
            fail();
            return null;
        }
    }

    private static void probe(final AppWithContainer app) throws InterruptedException {
        final AtomicReference<Throwable> ref = new AtomicReference<Throwable>();
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    app.start();
                } catch (final Exception e) {
                    ref.set(e);
                }
            }
        });

        t.start();
        app.stop();
        t.join();

        if (ref.get() != null) {
            throw new IllegalStateException(ref.get());
        }
    }
}
