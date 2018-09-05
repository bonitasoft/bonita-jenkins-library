#!groovy


/**
 * Ensure a string variable is defined and not empty.
 * Fail the build with an error message otherwise.
 */
def ensure_stringvar(var, error_message) {
    if (var == null || var.trim().isEmpty()) {
        error "[bcd-lib] ${error_message}"
    }
}

/**
 * Invoke the `bcd` command using a proper shell script.
 *
 * @param args String arguments to the `bcd` command
 */
def invoke_bcd(args) {
    // ensure args
    ensure_stringvar(args, "Arguments are empty! Did you forget to provide arguments to the 'bcd' step?")

    // ensure scenario
    def scenario = env.BCD_SCENARIO
    ensure_stringvar(scenario, "Mandatory environment variable BCD_SCENARIO is not set!")
    def bcd_cmd = "bcd -s ${scenario} -y ${args}"
    echo "[bcd-lib] ${bcd_cmd}"
    
    // execute bcd with bash
    sh """#!/bin/bash -l
set -euo pipefail

medium_echo() {
    echo "___________________________________________________________________________________________________________"
    echo "\$1"
}

medium_echo "Bonita Continuous Delivery for Jenkins!"
bcd version

cd \${BCD_HOME}
${bcd_cmd}
"""
}

/**
 * Make 'bcd' a Jenkins Pipeline step.
 * The `BCD_SCENARIO` environment variable MUST be defined.
 *
 * Configuration: 
 *   args  String arguments to the `bcd` command
 *   ignore_errors  Boolean flag to not fail the `bcd` command call uppon errors (default to false)
 * 
 * Example usage with a scripted pipeline:
 *
 *   node('bcd') {
 *     stage('Deploy Bonita Server') {
 *       bcd args: 'stack create', ignore_errors: true
 *       bcd args: 'stack undeploy', ignore_errors: true
 *       bcd args: 'stack deploy'
 *     }
 *
 *     stage('Build Bonita LivingApp') {
 *       bcd args: "livingapp build -p ${WORKSPACE} -e Qualification"
 *     }
 *   }
 *
 */
def call(Map config) {
    if (config.ignore_errors) {
        try {
            invoke_bcd(config.args)
        }
        catch (err) {
            echo """[bcd-lib] bcd call failed: ${err}
...ignore failure and continue..."""
        }
    } else {
        invoke_bcd(config.args)
    }
}
