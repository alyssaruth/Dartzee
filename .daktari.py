from daktari.checks.certs import *
from daktari.checks.docker import *
from daktari.checks.files import *
from daktari.checks.git import *
from daktari.checks.google import *
from daktari.checks.intellij_idea import *
from daktari.checks.java import *
from daktari.checks.kubernetes import *
from daktari.checks.misc import *
from daktari.checks.nodejs import *
from daktari.checks.onepassword import *
from daktari.checks.ssh import *
from daktari.checks.xml import *
from daktari.checks.yarn import *

daktari_version = "0.0.171"
title = "Dartzee"
using_asdf = ".asdf/shims" in os.getenv("PATH")
asdf_suggestion = "Run <cmd>./init-asdf.sh</cmd>"


class AwsSecretFilesPresent(FilesExist):
    name = "secrets.exist"
    file_paths = [
        "src/main/resources/AWS_LOGS",
        "src/main/resources/AWS_SYNC",
    ]
    pass_fail_message = "AWS credentials are <not/> present"
    suggestions = {
        OS.GENERIC: """
            Download the secret files from 1Password and place them in src/main/resources
            """
    }


checks = [
    GitInstalled(),
    JavaVersion(
        required_version=">=11.0.0",
        recommended_version="<12.0.0"
    ).suggest_if(using_asdf, asdf_suggestion),
    EnvVarSet(
        variable_name="JAVA_HOME",
        provision_command="Add export JAVA_HOME=... to ~/.bashrc or ~/.zshrc",
    ).suggest_if(using_asdf,
                 "Follow the instructions at https://github.com/halcyon/asdf-java?tab=readme-ov-file#java_home"),
    IntelliJIdeaInstalled(recommended_version=">=2022.3.2"),
    IntelliJProjectImported(),
    IntelliJProjectSdkJavaVersion(11),
    AwsSecretFilesPresent(),
]
