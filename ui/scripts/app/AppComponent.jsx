import React from "react";
import { createStore } from "redux";
import { Provider } from "react-redux";
import ReactDOM from "react-dom";
import axios from "axios";
import TagManager from "./views/TagManager.jsx";
import "event-source-polyfill";

class AppComponent {
  init = () => {
    this.initLoginRedirecting();
    this.initAppState();
    this.connectToSSEEndpoint();
    this.renderComponent();
  };
  initAppState = () => {
    const initialState = {
      tags: [],
    };
    const reducer = (state = initialState, action) => {
      const updatedState = { ...state };
      const actionType = action.type;

      if (actionType === "tags_updated") {
        updatedState["tags"] = action.data;
      }
      return updatedState;
    };
    this.store = createStore(reducer);
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
  updateReceived = (data) => {
    if (data["updateType"] === "tags") {
      this.store.dispatch({
        type: "tags_updated",
        data: data["updateData"],
      });
    }
  };
  connectToSSEEndpoint = () => {
    this.es = new EventSource("/api/es");
    this.es.addEventListener("message", this.onServerSideEvent);
  };
  onServerSideEvent = (event) => {
    if (event.type === "message") {
      this.updateReceived(JSON.parse(event.data));
    }
  };
  renderComponent = () => {
    const reactDiv = document.getElementById("reactDiv");
    if (reactDiv !== null) {
      ReactDOM.render(
        <Provider store={this.store}>
          <TagManager />
        </Provider>,
        reactDiv
      );
    }
  };
}

export default AppComponent;
