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


  .. tab:: Maven Central

     Add following dependencies to your pom:

     .. code-block:: xml
        :substitutions:

         <dependencies>
         ...
          <dependency>
            <groupId>io.github.opentelekomcloud-community</groupId>
            <artifactId>otc-sdk-v1-core</artifactId>
            <version>|pom_version|</version>
          </dependency>
        ...
        </dependencies>



API Usage
^^^^^^^^^

For API usage, see :api_usage:`API Usage guidelines <guidelines/>`.

For available REST APIs, see `REST API <https://docs.otc.t-systems.com/developer/api.html>`_ in Open Telekom Cloud Documentation.
