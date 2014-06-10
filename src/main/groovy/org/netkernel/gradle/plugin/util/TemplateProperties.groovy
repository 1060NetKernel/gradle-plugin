package org.netkernel.gradle.plugin.util

/**
 * TemplateProperties uses a map to store the actual properties used.  It mainly provides a collection
 * of augmenters that can take a property and generate additional properties for use in the templates.
 *
 * For example, moduleUrn is a property that is used to generate a number of other properties:
 * - moduleUrnAsPath
 * - moduleUrnAsPackage
 * - moduleUrnAsPackagePath
 *
 * Which augmenters are fired depends on the value being looked at.  Regular expressions are used to determine
 * if an augmenter should fire.
 */
class TemplateProperties {

    static final String MODULE_URN = 'moduleUrn'
    static final String DESTINATION_DIRECTORY = 'destinationDirectory'
    static final String NETKERNEL_TEMPLATE_DIRS = 'netkernel.template.dirs'

    Map<String, Object> templateProperties = [:]

    Object getProperty(String name) {
        return templateProperties.get(name)
    }

    void setProperty(String name, Object value) {
        templateProperties.put(name, value)

        // Augment properties as they are added
        this.@templateProperties.putAll(createAugmentedProperties(name, value))
    }

    void setProperties(Map p) {
        this.@templateProperties.putAll(p)
        p.each { key, value ->
            this.@templateProperties.putAll(createAugmentedProperties(key, value))
        }
    }

    Object each(Closure closure) {
        return templateProperties.each(closure)
    }

    /**
     * Augmenters create properties derived from others. Pattern matching is used to
     * ensure that they are only applied on appropriate properties. They only create
     * new properties and do not override already specified properties.
     *
     * @param templateProperties
     */
    Map createAugmentedProperties(String propertyName, Object propertyValue) {
        Map augmenters = [
            Path                    : [/^urn/, { it.replaceAll(":", ".") }],
            Package                 : [/^urn/, { it.substring(4).replaceAll(':', '.') }],
            PackagePath             : [/^urn/, { it.substring(4).replaceAll(':', '/') }],
            ResourcePath            : [/^urn/, { "res:/${it.substring(4).replaceAll(':', '/')}" }],
            Group                   : [/^urn/, { it.substring(4, it.lastIndexOf(':')).replaceAll(':', '.') }],
            ModuleXmlFriendlyVersion: [/^[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?$/, { it.replaceAll('-SNAPSHOT', '') }]
        ]

        Map augmentedProperties = [:]
        augmenters.each { name, augmenter ->
            // New property name adds 'As' to make the name make more sense (e.g. moduleUrnAsPath)
            String newPropertyName = "${propertyName}As${name}"

            // properties get tricky, so referencing the field directly (@templateProperties)
            if (!delegate.@templateProperties[newPropertyName]) {
                if (propertyValue =~ augmenter[0]) {
                    augmentedProperties.put(newPropertyName, augmenter[1](propertyValue))
                }
            }
        }
        return augmentedProperties
    }

}