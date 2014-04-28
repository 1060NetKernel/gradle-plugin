package org.netkernel.gradle.util

class TemplateProperties {

    static final String MODULE_URN = 'moduleUrn'
    static final String MODULE_NAME = 'moduleName'
    static final String MODULE_SPACE_NAME = 'moduleSpaceName'
    static final String MODULE_DESCRIPTION = 'moduleDescription'
    static final String MODULE_VERSION = 'moduleVersion'
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
            String newPropertyName = "${propertyName}As${name}"
            if (!delegate.@templateProperties[newPropertyName]) {
                if (propertyValue =~ augmenter[0]) {
                    augmentedProperties.put(newPropertyName, augmenter[1](propertyValue))
                }
            }
        }
        return augmentedProperties
    }

}