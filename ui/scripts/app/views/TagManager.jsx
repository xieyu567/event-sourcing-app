import React from "react";
import axios from "axios";
import { connect } from "react-redux";

class TagManager extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      text: "",
    };
  }

  componentDidMount = () => {
    axios.get("/api/tags").then(this.handleResponse);
  };

  handleResponse = (response) => {
    if (response.status === 200) {
      this.props.dispatch({
        text: "tags_updated",
        tags: response.data,
      });
    }
  };

  handleInput = (event) => {
    this.setState({
      text: event.target.value,
    });
  };

  handleKeyPress = (event) => {
    if (event.key === "Enter") {
      this.addTag();
    }
  };

  addTag = () => {
    const text = this.state.text;
    const isValid =
      this.props.tags.findIndex((el) => {
        return el.text === text;
      }) === -1;
    if (isValid) {
      axios.post("/api/createTag", { text }).then(() => {
        this.setState({
          text: "",
        });
      });
    }
  };

  deleteTag = (id) => {
    return () => {
      axios.post("/api/deleteTag", { id });
    };
  };

  render = () => {
    const tags = this.props.tags;
    return (
      <div className="tag-manager">
        <div className="tag-manager__input-panel">
          <div className="tag-manager__input-panel__input">
            <input
              type="text"
              className="form-control"
              onKeyPress={this.handleKeyPress}
              placeholder="Enter a new tag and press enter"
              value={this.state.text}
              onChange={this.handleInput}
            />
          </div>
        </div>
        <div className="tag-manager__cloud-panel">
          <div className="tag-manager__cloud-panel__available-tags">
            {tags.map((tag) => {
              return (
                <span className="badge badge-primary" key={tag.id}>
                  {tag.text}
                  <a
                    className="remove-tag-link"
                    onClick={this.deleteTag(tag.id)}
                  >
                    x
                  </a>
                </span>
              );
            })}
          </div>
        </div>
      </div>
    );
  };
}

const mapStateToProps = (state) => {
  return { tags: state.tags };
};

export default connect(mapStateToProps)(TagManager);
