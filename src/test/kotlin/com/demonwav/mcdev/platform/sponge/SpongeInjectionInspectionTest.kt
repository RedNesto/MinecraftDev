package com.demonwav.mcdev.platform.sponge

import com.demonwav.mcdev.platform.sponge.inspection.SpongeInjectionInspection
import org.intellij.lang.annotations.Language

class SpongeInjectionInspectionTest : BaseSpongeTest() {

    private fun doTest(@Language("JAVA") code: String) {
        buildProject {
            src {
                java("test/ASpongePlugin.java", code)
            }
        }

        myFixture.enableInspections(SpongeInjectionInspection::class.java)
        myFixture.checkHighlighting(false, false, false)
    }

    fun `test uninjectable type`() {
        doTest("""
package test;

import com.google.inject.Inject;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "a-plugin")
public class ASpongePlugin {
    @Inject
    private <error descr="String cannot be injected by Sponge.">String</error> string;
}
""")
    }

    fun `test path injection with @ConfigDir and @DefaultConfig`() {
        doTest("""
package test;

import com.google.inject.Inject;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;

@Plugin(id = "a-plugin")
public class ASpongePlugin {
    @Inject
    @ConfigDir(sharedRoot = false)
    @DefaultConfig(sharedRoot = false)
    private File <error descr="@ConfigDir and @DefaultConfig cannot be used on the same field.">file</error>;
}
""")
    }

    fun `test path injection without @ConfigDir`() {
        doTest("""
package test;

import com.google.inject.Inject;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;

@Plugin(id = "a-plugin")
public class ASpongePlugin {
    @Inject
    private File <error descr="An injected File must be annotated with either @ConfigDir or @DefaultConfig.">file</error>;
}
""")
    }

    fun `test invalid @DefaultConfig usage`() {
        doTest("""
package test;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "a-plugin")
public class ASpongePlugin {
    @Inject
    <error descr="Logger cannot be annotated with @DefaultConfig.">@DefaultConfig(sharedRoot = false)</error>
    private Logger logger;
}
""")
    }

    fun `test @ConfigDir on ConfigurationLoader`() {
        doTest("""
package test;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "a-plugin")
public class ASpongePlugin {
    @Inject
    @DefaultConfig(sharedRoot = false)
    <error descr="Injected ConfigurationLoader cannot be annotated with @ConfigDir.">@ConfigDir(sharedRoot = false)</error>
    private ConfigurationLoader<CommentedConfigurationNode> configurationLoader;
}
""")
    }

    fun `test ConfigurationLoader not annotated with @DefaultConfig`() {
        doTest("""
package test;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "a-plugin")
public class ASpongePlugin {
    @Inject
    private ConfigurationLoader<CommentedConfigurationNode> <error descr="Injected ConfigurationLoader must be annotated with @DefaultConfig.">configurationLoader</error>;
}
""")
    }

    fun `test ConfigurationLoader generic not CommentedConfigurationNode`() {
        doTest("""
package test;

import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "a-plugin")
public class ASpongePlugin {
    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<<error descr="Injected ConfigurationLoader generic parameter must be CommentedConfigurationNode.">ConfigurationNode</error>> configurationLoader;
}
""")
    }
}
