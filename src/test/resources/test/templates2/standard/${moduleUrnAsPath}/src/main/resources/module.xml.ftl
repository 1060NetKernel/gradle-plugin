<module version="2.0">
  <meta>
    <identity>
      <uri>${moduleUrn}</uri>
      <version>${moduleVersionAsModuleXmlFriendlyVersion}</version>
    </identity>
    <info>
      <name>${moduleSpaceName}</name>
      <description>${moduleDescription}</description>
      <icon>${moduleUrnAsResourcePath}/images/icon.png</icon>
    </info>
  </meta>

  <system>
    <dynamic/>
  </system>

  <rootspace uri="${moduleUrn}" name="${moduleSpaceName}">

    <fileset>
      <regex>${moduleUrnAsResourcePath}/images/(.*)</regex>
      <rewrite>res:/resources/images/$1</rewrite>
    </fileset>
  </rootspace>

</module>