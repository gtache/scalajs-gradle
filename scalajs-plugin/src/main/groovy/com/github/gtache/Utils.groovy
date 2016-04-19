package com.github.gtache

import org.gradle.api.Project
import org.scalajs.core.tools.linker.backend.OutputMode
import org.scalajs.core.tools.logging.Level
import org.scalajs.jsenv.JSEnv
import org.scalajs.jsenv.nodejs.NodeJSEnv
import org.scalajs.jsenv.phantomjs.PhantomJSEnv
import org.scalajs.jsenv.rhino.RhinoJSEnv
import scala.collection.Map$
import scala.collection.Seq$
import scala.collection.immutable.List$


public class Utils {

    private Utils() {}

    /**
     * Resolves the level of logging, depending on the project properties
     * @param project The project with the property to check
     * @param property The property used on the switch
     * @return The level of logging (default : Debug)
     */
    public static Level resolveLogLevel(Project project, String property, Level base) {
        def level = base
        if (project.hasProperty(property)) {
            switch (project.property(property)) {
                case 'Error':
                    level = Level.Error$.MODULE$
                    break
                case 'Warn':
                    level = Level.Warn$.MODULE$
                    break
                case 'Info':
                    level = Level.Info$.MODULE$
                    break
                case 'Debug':
                    level = Level.Debug$.MODULE$
                    break
                default:
                    project.logger.warn("Unknown log level : " + project.property(property))
                    break
            }
        }
        return level
    }

    /**
     * Resolves the environment to use, depending on the project properties
     * @param project The project with properties to check
     * @return The environment to use (Default : Node)
     */
    public static JSEnv resolveEnv(Project project) {
        def env
        if (project.hasProperty('rhino')) {
            env = new RhinoJSEnv(Scalajsld$.MODULE$.options().semantics(), false)
        } else if (project.hasProperty('phantom')) {
            env = new PhantomJSEnv("phantomjs", List$.MODULE$.empty(), Map$.MODULE$.empty(), true, null)
        } else {
            env = new NodeJSEnv("node", Seq$.MODULE$.empty(), Map$.MODULE$.empty())
        }
        return env
    }

    public static OutputMode getOutputMode(String s) {
        if (s == "ECMAScript51Global") {
            return OutputMode.ECMAScript51Global$.MODULE$
        } else if (s == "ECMAScript51Isolated") {
            return OutputMode.ECMAScript51Isolated$.MODULE$
        } else if (s == "ECMAScript6") {
            return OutputMode.ECMAScript6$.MODULE$
        } else {
            return null
        }
    }
}
