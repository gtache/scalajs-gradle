package com.github.gtache.tasks

import com.github.gtache.Scalajsp
import com.github.gtache.ScalaUtils$
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskAction
import scala.Option
import scala.collection.JavaConverters

import static com.github.gtache.ScalaUtils.CPSeparator

/**
 * Task used to call scalajsp
 */
class ScalajspTask extends DefaultTask {
    final String description = "Translates and prints sjsir file to a more readable format"

    public static final String MIN_SUPPORTED = 's'
    public static final String SUPPORTED = 'supported'
    public static final String MIN_INFOS = 'i'
    public static final String INFOS = 'info'
    public static final String MIN_FILENAME = 'f'
    public static final String FILENAME = 'filename'
    public static final String MIN_JAR = 'j'
    public static final String JAR = 'jarfile' //can't use jar as it is the name of a task apparently...

    FileCollection srcFiles
    /**
     * Parametrize the options and calls scalajsp
     */
    @TaskAction
    def run() {
        if (project.hasProperty(MIN_SUPPORTED) || project.hasProperty(SUPPORTED)) {
            Scalajsp.printSupported()
        } else {
            def cp = project.configurations.runtime + srcFiles
            def sjsirs = SjsirTask.listSJSIR(cp)
            Scalajsp.Options options = Scalajsp.defaultOptions()
            if (project.hasProperty(MIN_INFOS) || project.hasProperty(INFOS)) {
                options = options.withInfos(true)
            }
            if (project.hasProperty(MIN_FILENAME)) {
                String[] filenames = (project.property(MIN_FILENAME) as String).split(CPSeparator())
                filenames = resolveFilenames(filenames, sjsirs)
                options = options.withFileNames(JavaConverters.asScalaSet(filenames.toList().toSet())
                        .toIndexedSeq())
            } else if (project.hasProperty(FILENAME)) {
                String[] filenames = (project.property(FILENAME) as String).split(CPSeparator())
                filenames = resolveFilenames(filenames, sjsirs)
                options = options.withFileNames(JavaConverters.asScalaSet(filenames.toList().toSet())
                        .toIndexedSeq())
            }
            if (project.hasProperty(MIN_JAR)) {
                options = options.withJar(Option.apply(project.file(project.property(MIN_JAR))))
            } else if (project.hasProperty(JAR)) {
                options = options.withJar(Option.apply(project.file(project.property(JAR))))
            }
            Scalajsp.execute(options)
        }
    }

    private static def putAndCheck(Map<String, String> map, String key, String value, Set<String> forbidden) {
        def strippedKey = ScalaUtils$.MODULE$.stripRight(key, ".sjsir")
        if (!forbidden.contains(strippedKey)) {
            String ret = map.putIfAbsent(strippedKey, value)
            if (ret != null) {
                map.remove(strippedKey)
                forbidden.add(strippedKey)
            }
        }
    }

    private static String[] resolveFilenames(String[] filenames, List<Tuple2<String, String>> sjsirsCP) {
        def mapping = new HashMap<String, String>()
        def forbidden = new HashSet<String>()
        sjsirsCP.each {
            putAndCheck(mapping, it.first, it.second, forbidden)
            putAndCheck(mapping, it.first.replace("\$", ""), it.second, forbidden)
            putAndCheck(mapping, it.first.split("\\.").last(), it.second, forbidden)
            putAndCheck(mapping, it.first.split("\\.").last().replace("\$", ""), it.second, forbidden)
        }
        return filenames.collect {
            if (mapping.containsKey(it)) {
                mapping.get(it)
            } else {
                it
            }
        }

    }
}
