/**
 * Kubernetes Manager
 * 
 * @galasa.manager Kubernetes
 * 
 * @galasa.release.state ALPHA - This Manager is being actively developed. It is subject to change and has not been extensively tested.
 * 
 * @galasa.description
 * 
 * This Manager provides a test with a Kubernetes Namespace to utilize. The test will provide YAML representations
 * of the resources that the test requires.
 * <br><br>
 * As an absolute minimum, the CPS property <code>kubernetes.cluster.K8S.url</code> must be provided and a credential
 * <code>secure.credentials.K8S.token</code> for the API token.
 * <br><br>
 * The Kubernetes Manager supports Galasa Shared Environments. Shared environments provide 
 * the ability to create a test environment that can be shared across multiple test runs 
 * so you don't have to provision a test environment for each test.  
 * 
 * @galasa.limitations
 * 
 * The Manager only supports the following Kubernetes resources:-<br>
 * <br>
 * - Deployment<br>
 * - StatefulSet<br>
 * - Service<br>
 * - Secret<br>
 * - ConfigMap<br>
 * - PersistentVolumeClaim<br>
 * <br>
 * 
 * If additional resources are required, please raise a GitHub issue.
 */
package dev.galasa.kubernetes;
