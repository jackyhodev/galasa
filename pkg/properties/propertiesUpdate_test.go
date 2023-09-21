/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package properties

import (
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/galasa.dev/cli/pkg/utils"
	"github.com/stretchr/testify/assert"
)

// MockServlet
func newPropertiesServletMock(t *testing.T) *httptest.Server {

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		mockPropertiesServlet(t, w, r)
	}))

	return server
}

func mockPropertiesServlet(t *testing.T, w http.ResponseWriter, r *http.Request) {
	if !strings.Contains(r.URL.Path, "/cps/") {
		t.Errorf("Expected to request '/cps/', got: %s", r.URL.Path)
	}
	if r.Header.Get("Accept") != "application/json" {
		t.Errorf("Expected Accept: application/json header, got: %s", r.Header.Get("Accept"))
	}
	w.Header().Set("Content-Type", "application/json")

	var statusCode int
	var namespaceProperties string
	splitUrl := strings.Split(r.URL.Path, "/")
	namespace := splitUrl[2]

	//cps/ns/properties/name
	statusCode, namespaceProperties = CheckNamespace(namespace)
	if len(splitUrl) == 5 {

		if namespace == "invalidNamespace" {
			statusCode = 404
			namespaceProperties = `{
				error_code: 5017,
				error_message: "GAL5017E: Error occured when trying to access namespace 'invalidNamespace'. The Namespace provided is invalid."
				}`
		} else if namespace == "validNamespace" {
			propertyName := splitUrl[4]
			if propertyName == "invalidProperty" {
				statusCode = 404
				namespaceProperties = `{
					error_code: 5018
					error_message: "GAL5018E: Error occured when trying to access property 'propertyName'. The property name provided is invalid."
					}`
			} else if propertyName == "validProperty" {
				statusCode = 201
				namespaceProperties = `[
					{
						"name" = "validProperty",
						"value" = "newValue"
					}
				]`
			}
		}
	}
	w.WriteHeader(statusCode)
	w.Write([]byte(namespaceProperties))
}

func TestUpdatePropertyValueReturnsOk(t *testing.T) {
	//Given...
	namespace := "validNamespace"
	name := "validName"
	value := "newValue"

	server := newPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	console := utils.NewMockConsole()
	expectedOutput := "Successfully updated the value of " + name + " to " + value

	//When
	err := UpdateProperty(namespace, name, value, apiServerUrl, console)

	//Then
	assert.Nil(t, err)
	assert.Equal(t, expectedOutput, console.ReadText())
}

// invalid OR empty namespace, valid propertyname
func TestUpdatePropertyWithInvalidNamesapceReturnsError(t *testing.T) {
	//Given...
	namespace := "invalidNamespace"
	name := "validName"
	value := "newValue"

	server := newPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	console := utils.NewMockConsole()

	//When
	err := UpdateProperty(namespace, name, value, apiServerUrl, console)

	//Then
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "GAL5017E")
}

// validnamespace , invalid propertyname
func TestValidNamespaceAndInvalidPropertyNameReturnsError(t *testing.T) {
	//Given...
	namespace := "validNamespace"
	name := "invalidName"
	value := "newValue"

	server := newPropertiesServletMock(t)
	apiServerUrl := server.URL
	defer server.Close()

	console := utils.NewMockConsole()
	// expectedOutput := `{
	// 	error_code: 5018
	// 	error_message: "GAL5018E: Error occured when trying to access property 'propertyName'. The property name provided is invalid."
	// }`

	//When
	err := UpdateProperty(namespace, name, value, apiServerUrl, console)

	//Then
	assert.Error(t, err)
	assert.ErrorContains(t, err, "GAL5018E:")
}

//create new property successful
//unsuccessful
//check if propertyname of the same name value? -> error?
//property fails to create because not in right format
