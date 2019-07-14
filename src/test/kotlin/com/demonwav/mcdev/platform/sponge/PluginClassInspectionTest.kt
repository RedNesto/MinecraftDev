package com.demonwav.mcdev.platform.sponge

import com.demonwav.mcdev.platform.sponge.inspection.SpongePluginClassInspection
import org.intellij.lang.annotations.Language

class PluginClassInspectionTest : BaseSpongeTest() {

    private fun doTest(@Language("JAVA") code: String) {
        buildProject {
            src {
                java("test/ASpongePlugin.java", code)
            }
        }

        myFixture.enableInspections(SpongePluginClassInspection::class.java)
        myFixture.checkHighlighting(false, false, false)
    }

    fun `test invalid plugin id`() {
        doTest("""
package test;

import org.spongepowered.api.plugin.Plugin;

@Plugin(id = <error descr="Plugin IDs should be lowercase, and only contain characters from a-z, dashes or underscores, start with a lowercase letter, and not exceed 64 characters.">"a plugin"</error>)
public class ASpongePlugin {
    ASpongePlugin() {
    }
}
""")
    }

    fun `test no empty constructor`() {
        doTest("""
package test;

import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "a-plugin")
public class <error descr="Plugin class must have an empty constructor or an @Inject constructor.">ASpongePlugin</error> {
}
""")
    }

    fun `test private constructor`() {
        doTest("""
package test;

import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "a-plugin")
public class ASpongePlugin {
    private <error descr="Plugin class empty constructor must not be private.">ASpongePlugin</error>() {
    }
}
""")
    }

    fun `test private constructor with injected constructor`() {
        doTest("""
package test;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "a-plugin")
public class ASpongePlugin {
    private ASpongePlugin() {
    }

    @Inject
    private ASpongePlugin(Logger logger) {
    }
}
""")
    }

    fun `test no private constructor with injected constructor`() {
        doTest("""
package test;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "a-plugin")
public class ASpongePlugin {
    @Inject
    private ASpongePlugin(Logger logger) {
    }
}
""")
    }
}
