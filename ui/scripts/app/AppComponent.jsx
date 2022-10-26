import React from "react";
import ReactDOM from "react-dom";
import axios from "axios";
import TagManager from "./views/TagManager.jsx";
import "event-source-polyfill";

class AppComponent {
  init = () => {
    this.initLoginRedirecting();
    this.renderComponent();
    this.connectToSSEEndpoint();
  };
  initLoginRedirecting = () => {
    axios.interceptors.response.use(
      (response) => {
        return response;
      },
      (error) => {
        if (error.response.status === 401) {
          window.location = "/login";
        }
        return Promise.reject(error);
      }
    );
  };
  connectToSSEEndpoint = () => {
    this.es = new EventSource("/api/es");
    this.es.addEventListener("message", (event) => {
      if (event.type === "message") {
        console.log("Message received", event.data);
      }
    });
  };
  renderComponent = () => {
    const reactDiv = document.getElementById("reactDiv");
    if (reactDiv !== null) {
      ReactDOM.render(<TagManager />, reactDiv);
    }
  };
}

export default AppComponent;
