Samples
=======

.. toctree::
   :maxdepth: 3

To run samples following environment variables have to be set:

.. list-table:: Environment variables
    :widths: 20 20 25
    :header-rows: 1

    * - Name
      - Value
      - Remark

    * - OTC_SDK_AK
      - Access key
      - see: :api_usage:`Generating an AK and SK<guidelines/calling_apis/ak_sk_authentication/generating_an_ak_and_sk.html>` in API usage guide.

    * - OTC_SDK_SK
      - Secret key
      - see: :api_usage:`Generating an AK and SK<guidelines/calling_apis/ak_sk_authentication/generating_an_ak_and_sk.html>` in API usage guide.

    * - OTC_SDK_REGION
      - Region (e.g. eu-de)
      - for available regions, see: `Regions <https://docs.otc.t-systems.com/regions-and-endpoints/index.html#region>`_.

    * - OTC_SDK_PROJECTID
      - Project Id
      - see: :api_usage:`Obtaining a Project ID<guidelines/calling_apis/obtaining_required_information.html>` in API usage guide.

    * - OTC_IAM_ENDPOINT
      - IAM Endpoint
      - e.g. https://iam.eu-de.otc.t-systems.com or https://iam.eu-nl.otc.t-systems.com

For samples, see :github_repo_master:`Samples folder <samples>`

.. note:: 
  Samples in folder :github_repo_master:`demo <samples/src/main/java/com/otc/sdk/samples/demo>`
  are to demonstrate on how to use the OTC SDK for various use cases, including HTTP requests and WebSocket communication.
  They are not working examples and require additional setup or configuration to run successfully.
