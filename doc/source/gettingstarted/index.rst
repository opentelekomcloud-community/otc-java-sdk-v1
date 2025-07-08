Getting started with the OTC Java SDK v1
========================================

.. toctree::
   :maxdepth: 3


Installing libraries
^^^^^^^^^^^^^^^^^^^^

These libraries are available through:

.. tabs::

  .. tab:: Source

     Install libraries to local .m2 repository

     .. code-block:: shell
        :substitutions:

        # Check out the repository with tag v|pom_version|
        git clone --branch v|pom_version| --single-branch |github_repo|

        # change to folder
        cd otc-java-sdk-v1

        # run maven clean install
        mvn clean install

  .. tab:: GitHub and Maven

    To use GitHub maven repository modify your ``settings.xml`` (e.g. ~/.m2/settings.xml) as follows.

    If you need to use a proxy for internet connections, see `Configuring a proxy <https://maven.apache.org/guides/mini/guide-proxies.html>`_.

    The environment variable ``GITHUB_TOKEN`` has to be set with your `GitHub personal access token <https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-with-a-personal-access-token>`_,
    e.g. in your ``~/.profile``.

     Detailed instructions can be found `Working with the Apache Maven registry: Installing a package <https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#installing-a-package>`_

     .. code-block:: xml
        :caption: settings.xml

        <servers>
          <server>
            <id>github.opentelekomcloud-community</id>
            <configuration>
              <httpHeaders>
                <property>
                  <name>Authorization</name>
                  <value>Bearer ${env.GITHUB_TOKEN}</value>
                </property>
              </httpHeaders>
            </configuration>
        </servers>

        <profiles>
          <profile>
            <id>default</id>

            <repositories>
              <repository>
                <id>central</id>
                <url>https://repo1.maven.org/maven2</url>
              </repository>

              <repository>
                <id>github.opentelekomcloud-community</id>
                <url>https://maven.pkg.github.com/opentelekomcloud-community/otc-java-sdk-v1</url>

                <releases>
                  <enabled>true</enabled>
                  <updatePolicy>daily</updatePolicy>
                </releases>

                <snapshots>
                  <enabled>true</enabled>
                  <updatePolicy>always</updatePolicy>
                </snapshots>

              </repository>
            </repositories>
          </profile>
        </profiles>

        <activeProfiles>
          <activeProfile>default</activeProfile>
        </activeProfiles>


  .. tab:: Maven Central

     Comming soon ...


API Usage
^^^^^^^^^

For API usage, see :api_usage:`API Usage guidelines <guidelines/>`.

For available REST APIs, see `REST API <https://docs.otc.t-systems.com/developer/api.html>`_ in Open Telekom Cloud Documentation.
